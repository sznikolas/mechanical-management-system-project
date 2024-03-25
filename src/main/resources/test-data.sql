INSERT INTO `user` (`id`, `email`, `first_name`, `non_locked`, `verified_email`, `last_name`, `password`,
                    `registration_date`)
VALUES ('1', 'admin@gmail.com', 'Admin', 1, 1, 'Account', '{noop}admin', '2024-03-20 15:32:57.141789');

INSERT INTO `user` (`id`, `email`, `first_name`, `non_locked`, `verified_email`, `last_name`, `password`,
                    `registration_date`)
VALUES ('2', 'user@gmail.com', 'User', 1, 1, 'Account', '{noop}user', '2024-03-20 15:32:57.141789');


INSERT INTO `role` (`id`, `role_name`)
VALUES ('1', 'ADMIN');

INSERT INTO `role` (`id`, `role_name`)
VALUES ('2', 'USER');

INSERT INTO `role` (`id`, `role_name`)
VALUES ('3', '1_Mechanical Management S. 01_Hungary');
INSERT INTO `role` (`id`, `role_name`)
VALUES ('4', '2_Mechanical Management S. 02_Slovakia');
INSERT INTO `role` (`id`, `role_name`)
VALUES ('5', '3_Mechanical Management S. 03_Austria');


INSERT INTO `user_roles` (`role_id`, `user_id`)
VALUES ('1', '1');
INSERT INTO `user_roles` (`role_id`, `user_id`)
VALUES ('3', '1');
INSERT INTO `user_roles` (`role_id`, `user_id`)
VALUES ('4', '1');
INSERT INTO `user_roles` (`role_id`, `user_id`)
VALUES ('5', '1');

INSERT INTO `user_roles` (`role_id`, `user_id`)
VALUES ('2', '2');
INSERT INTO `user_roles` (`role_id`, `user_id`)
VALUES ('3', '2');
INSERT INTO `user_roles` (`role_id`, `user_id`)
VALUES ('4', '2');



INSERT INTO `company` (`company_id`, `company_name`, `country`, `company_description`, `company_id_number`, `location`, `post_code`, `registration_date`, `street`, `tin`, `vat_id`, `deputy_leader_id`, `owner_id`)
VALUES ('1', 'Mechanical Management S. 01', 'Hungary', 'Company description', '64518156414548', 'Budapest', '4454', '2024-03-20 15:32:57.141789', 'Király utca', '64518156414548', 'HU64518156414548', '2', '1');
INSERT INTO `company` (`company_id`, `company_name`, `country`, `company_description`, `company_id_number`, `location`, `post_code`, `registration_date`, `street`, `tin`, `vat_id`, `deputy_leader_id`, `owner_id`)
VALUES ('2', 'Mechanical Management S. 02', 'Slovakia', 'Company description', '5484146541', 'Bratislava', '3324', '2024-03-20 18:32:57.141789', 'Bartokova ulica', '5484146541', 'SK5484146541', '1', '1');
INSERT INTO `company` (`company_id`, `company_name`, `country`, `company_description`, `company_id_number`, `location`, `post_code`, `registration_date`, `street`, `tin`, `vat_id`, `deputy_leader_id`, `owner_id`)
VALUES ('3', 'Mechanical Management S. 03', 'Austria', 'Company description', '84517513', 'Vien', '3324', '2024-03-20 18:32:57.141789', 'Hollgasse', '84517513', 'AUT84517513', '1', '1');

INSERT INTO `company_roles` (`company_id`, `id`, `role_id`)
VALUES ('1', '1', 3);
INSERT INTO `company_roles` (`company_id`, `id`, `role_id`)
VALUES ('2', '2', 4);
INSERT INTO `company_roles` (`company_id`, `id`, `role_id`)
VALUES ('3', '3', 5);

INSERT INTO `machines` (`id`, `charged_amount`, `machine_description`, `last_modified_date`, `is_active`, `machine_brand`, `machine_model`, `machine_parts_sum`, `profit`,`registration_date`, `tax_in_percent`, `company_id`)
VALUES ('1', '375', 'Description', '2024-03-20 15:32:57.141789', 1, 'Renault', 'Megane', '200', '175', '2024-03-20 15:32:57.141789', '0', '1');

INSERT INTO `machine_parts` (`machine_part_price`, `part_price_with_tax`, `tax_in_percent`, `unit_tax`, `id`, `machine_id`, `mpart_registration_date`, `machine_part`)
VALUES ('100', '100', '0', '0', '1', '1', '2024-03-20 16:32:57.141789', 'Something');
INSERT INTO `machine_parts` (`machine_part_price`, `part_price_with_tax`, `tax_in_percent`, `unit_tax`, `id`, `machine_id`, `mpart_registration_date`, `machine_part`)
VALUES ('100', '100', '0', '0', '2', '1', '2024-03-20 16:33:57.141789', 'Something2');


INSERT INTO `machines` (`id`, `charged_amount`, `machine_description`, `last_modified_date`, `is_active`, `machine_brand`, `machine_model`, `machine_parts_sum`, `profit`,`registration_date`, `tax_in_percent`, `company_id`)
VALUES ('2', '500', 'Description', '2024-03-20 18:38:57.141789', 1, 'Fiat', 'Bravo 2', '240', '260', '2024-03-20 15:32:57.141789', '20', '1');

INSERT INTO `machine_parts` (`machine_part_price`, `part_price_with_tax`, `tax_in_percent`, `unit_tax`, `id`, `machine_id`, `mpart_registration_date`, `machine_part`)
VALUES ('100', '120', '20', '20', '3', '2', '2024-03-20 16:32:57.141789', 'Something');
INSERT INTO `machine_parts` (`machine_part_price`, `part_price_with_tax`, `tax_in_percent`, `unit_tax`, `id`, `machine_id`, `mpart_registration_date`, `machine_part`)
VALUES ('100', '120', '20', '20', '4', '2', '2024-03-20 16:33:57.141789', 'Something2');

INSERT INTO `machines` (`id`, `charged_amount`, `machine_description`, `last_modified_date`, `is_active`, `machine_brand`, `machine_model`, `machine_parts_sum`, `profit`,`registration_date`, `tax_in_percent`, `company_id`)
VALUES ('3', '50', 'Description', '2024-03-20 18:32:57.141789', 0, 'Opel', 'Astra', '0', '125', '2024-03-20 15:32:57.141789', '0', '1');

INSERT INTO `machines` (`id`, `charged_amount`, `machine_description`, `last_modified_date`, `is_active`, `machine_brand`, `machine_model`, `machine_parts_sum`, `profit`,`registration_date`, `tax_in_percent`, `company_id`)
VALUES ('4', '0', 'Description', '2024-03-20 18:32:57.141789', 1, 'Ferrari', 'Enzo', '0', '0', '2024-03-20 15:32:57.141789', '0', '2');
INSERT INTO `machines` (`id`, `charged_amount`, `machine_description`, `last_modified_date`, `is_active`, `machine_brand`, `machine_model`, `machine_parts_sum`, `profit`,`registration_date`, `tax_in_percent`, `company_id`)
VALUES ('5', '485', 'Description', '2024-03-20 18:32:57.141789', 1, 'BMW', 'X5', '0', '485', '2024-03-20 15:32:57.141789', '0', '2');

INSERT INTO `machines` (`id`, `charged_amount`, `machine_description`, `last_modified_date`, `is_active`, `machine_brand`, `machine_model`, `machine_parts_sum`, `profit`,`registration_date`, `tax_in_percent`, `company_id`)
VALUES ('6', '750', 'Description', '2024-03-20 18:32:57.141789', 1, 'Ford', 'Fiesta', '247.30', '502.70', '2024-03-20 15:45:57.141789', '0', '3');
INSERT INTO `machine_parts` (`machine_part_price`, `part_price_with_tax`, `tax_in_percent`, `unit_tax`, `id`, `machine_id`, `mpart_registration_date`, `machine_part`)
VALUES ('247.30', '247.30', '0', '0', '5', '6', '2024-03-20 16:32:57.141789', 'Something');



INSERT INTO `job_application` (`id`, `accepted`, `company_id`, `employee_id`)
VALUES ('1', 1, '1', '2');
INSERT INTO `job_application` (`id`, `accepted`, `company_id`, `employee_id`)
VALUES ('2', 1, '2', '2');
INSERT INTO `job_application` (`id`, `accepted`, `company_id`, `employee_id`)
VALUES ('3', 0, '3', '2');

INSERT INTO `company_employees` (`company_id`, `user_id`)
VALUES ('1', '2');
INSERT INTO `company_employees` (`company_id`, `user_id`)
VALUES ('2', '2');